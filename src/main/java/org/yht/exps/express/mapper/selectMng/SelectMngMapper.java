package org.yht.exps.express.mapper.selectMng;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.selectMng.DeleteSelfSelection;
import org.yht.exps.express.model.selectMng.GoodsSelectionInfo;
import org.yht.exps.express.model.selectMng.GoodsSelectionMail;
import org.yht.exps.express.model.selectMng.SaveGoodsSelectionInfo;
import org.yht.exps.express.model.selectMng.Selection;
import org.yht.exps.express.model.selectMng.SelfSelection;
import org.yht.exps.express.model.selectMng.UpdateSelectionList;
import org.yht.exps.express.model.selectMng.search.SearchGoodsSelectionInfo;
import org.yht.exps.express.model.selectMng.search.SearchSelectionList;
import org.yht.exps.express.model.selectMng.search.SearchSelfSelection;
import org.yht.exps.express.model.selectMng.search.SearchSelfSelectionList;

@Mapper
public interface SelectMngMapper {
	
	
	List<Selection> selectSelectionList(SearchSelectionList search);
	List<Selection> selectTempSelectionList(SearchSelectionList search);
	List<GoodsSelectionInfo> selectTempSelectionItemList(SearchSelectionList search);
	List<Selection> selectSelectionOdList(UpdateSelectionList search);
	List<Map<String, Object>> selectSelectionChkList(SearchSelectionList search);
	void updateSelectionList(Selection search);
	void updateInspectionList(Selection search);
	void updateSelectionClearanceStatus(Selection search);
	void updateSelectionYn(UpdateSelectionList search);
	void updateSelectionHsList(GoodsSelectionInfo goodsSelectionInfo);
	void insertTempSelection(Selection selection);
	void insertTempSelectionItem(GoodsSelectionInfo selection);
	void insertTempSelectionItemHbl(Selection selection);
	void updateTempSelectionItem(GoodsSelectionInfo selection);
	void deleteTempSelection(Map<String, Object> map);
	void deleteOtherTempSelection(Map<String, Object> map);
	void deleteTempSelectionItem(Map<String, Object> map);
	String selectSelectionYn(UpdateSelectionList search);
	
	Map<String, Object> insertGoodsSelectionInfo(GoodsSelectionInfo goodsSelectionInfo);
	void insertGoodsSelectionInfoBatch(Map<String, Object> goodsSelectionInfo);
	void insertGoodsSelectionInfoBackupFile(GoodsSelectionInfo goodsSelectionInfo);
	void deleteGoodsSelectionInfo(SaveGoodsSelectionInfo goodsSelectionInfo);
	void updateGoodsSelectionInfo(Map<String, Object> goodsSelectionInfo);
	List<GoodsSelectionInfo> selectItenName(Selection search);
	List<Map<String, Object>> selectChkItemNameList(SearchSelectionList search);
	List<Map<String, Object>> selectChkHsCodeList(SearchSelectionList search);
	List<Map<String, Object>> selectChkOtherList(SearchSelectionList search);
//	List<Map<String, Object>> selectGoodsSelectionList(Map<String, Object> search);
	List<GoodsSelectionInfo> selectGoodsSelectionList(SearchGoodsSelectionInfo search);
	String selectGoodsSelectionDupChk(Map<String, Object> search);
	
	
	void insertSelfSelectionExcelUpload(Map<String, Object> selfSelection);
	void insertSelfSelection(SelfSelection selfSelection);
	void updateSelfSelection(SelfSelection selfSelection);
	void deleteSelfSelection(DeleteSelfSelection delete);
	SelfSelection selectSelfSelection(SelfSelection selfSelection);
	SelfSelection selectSelfSelection(SearchSelfSelection search);
	List<Selection> selectInspectionList(Map<String, Object> map);
	List<SelfSelection> selectSelfSelectionList(SearchSelfSelectionList search);
	List<Map<String, Object>> selectSelfSelectionListExcelUploadChk();
	List<Map<String, Object>> selectSelectionMailList(UpdateSelectionList search);
	List<GoodsSelectionMail> selectGoodsSelectionMailList(SearchSelectionList search);
	
	void updateSelectionSimpleDocNo(Selection selection);
}
