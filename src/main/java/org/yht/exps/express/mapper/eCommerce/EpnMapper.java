package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.epn.Epn;
import org.yht.exps.express.model.eCommerce.epn.search.SearchEpnList;

@Mapper
public interface EpnMapper {
	
	List<Epn> selectEpnList(SearchEpnList search);
	void insertEpn(Epn epn);
	void insertEpnBatch(Epn epn);
	void updateEpn(Epn epn);
	void deleteEpn(Epn epn);
	Epn selectEpn(Epn epn);
	
	List<String> selectCountryCode(Map<String, Object> map);
	List<Map<String, Object>> selectEpnChk(Map<String, Object> map);
	void updateEpnExcelUpload(Map<String, Object> map);
	void insertEpnExcelUpload(Map<String, Object> map);

    String selectManifestLicenseTargetCount(SearchEpnList search);

    List<Map<String, Object>> selectManifestLicenseList(SearchEpnList search);
    void updateOdLicenseInfo(Map<String, Object> paramMap);
    Map<String, Object> selectOdLicenseInfo(Map<String, Object> paramMap);
    void insertOdLicenseInfo(Map<String, Object> paramMap);
    void updateEpnMfTsYn(Map<String, Object> paramMap);
    void updateOdIert(Map<String, Object> paramMap);
    void updateOdShipperInfo(Map<String, Object> paramMap);
}
