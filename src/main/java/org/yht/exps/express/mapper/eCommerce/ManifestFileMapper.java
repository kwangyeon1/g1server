package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.order.ManifestFile;
import org.yht.exps.express.model.eCommerce.order.search.SearchManifestFileList;
import org.yht.exps.express.model.edi.ManifestInHouse;
import org.yht.exps.express.model.edi.search.SearchManifestInList;

@Mapper
public interface ManifestFileMapper {
	List<ManifestFile> selectExclusionList(SearchManifestFileList search);
	List<ManifestFile> selectMasterTempList(SearchManifestFileList search);
	List<ManifestFile> selectUpsBasicList(SearchManifestFileList search);
	List<ManifestFile> selectUpsGroupList(SearchManifestFileList search);
	List<String> selectMasterTempGroupName(SearchManifestFileList search);
	void deleteExclusion(SearchManifestFileList search);
	void insertExclusion(Map<String, Object> map);
	void updateOrderExclusion(Map<String, Object> map);
	void updateExclusion(Map<String, Object> map);
	Map<String, Object> selectExclusionOrderUpdate(Map<String, Object> map);
	Map<String, Object> selectExclusion(Map<String, Object> map);
	Map<String, Object> selectMasterInfo(Map<String, Object> map);
	Map<String, Object> selectOrderCustomsId(Map<String, Object> map);
	SearchManifestFileList selectMasterInfo2(Map<String, Object> map);
	List<Map<String, Object>> selectHouseInfoList(Map<String, Object> map);
	List<Map<String, Object>> selectEpnList(Map<String, Object> map);
	List<Map<String, Object>> selectUpsBasic(Map<String, Object> map);
	Map<String, Object> selectUpsBasicMap(Map<String, Object> map);
	List<Map<String, Object>> selectUpsBound(Map<String, Object> map);
	List<Map<String, Object>> selectUpsGroup(Map<String, Object> map);
	Map<String, Object> selectBusEdiCodeInfo(Map<String, Object> map);
	ManifestFile selectUpsBasicEntry(ManifestFile search);
	ManifestFile selectUpsGroupEntry(ManifestFile search);
	List<String> selectUpsBasicDupCheck(Map<String, Object> map);
	void insertMasterTemp(ManifestFile manifestFile);
	void updateMasterTemp(ManifestFile manifestFile);
	void deleteMasterTemp(ManifestFile manifestFile);
	void insertUpsBasic(ManifestFile manifestFile);
	void insertUpsBasicMap(Map<String, Object> map);
	void updateUpsBasic(ManifestFile manifestFile);
	void deleteUpsBasic(ManifestFile manifestFile);
	void insertUpsGroup(ManifestFile manifestFile);
	void updateUpsGroup(ManifestFile manifestFile);
	void deleteUpsGroup(ManifestFile manifestFile);
	void insertMasterOrder(SearchManifestFileList search);
	void insertHouseOrder(ManifestFile manifestFile);
	void insertHouseItem(Map<String, Object> map);
	void insertHousePacking(Map<String, Object> map);
	void insertOdChildNo(Map<String, Object> map);
	List<ManifestFile> selectTsHouseInList(SearchManifestFileList search);
	void insertTsHouseOrder(ManifestFile manifestFile);
	void updateTsHouseOrder(ManifestFile manifestFile);
	int selectDuplicateHblNo(Map<String, Object> odMap);
}
