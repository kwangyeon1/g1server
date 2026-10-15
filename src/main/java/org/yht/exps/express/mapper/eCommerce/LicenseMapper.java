package org.yht.exps.express.mapper.eCommerce;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.yht.exps.express.model.eCommerce.license.License;
import org.yht.exps.express.model.eCommerce.license.LicenseCommodityItem;
import org.yht.exps.express.model.eCommerce.license.LicenseGoodsItem;
import org.yht.exps.express.model.eCommerce.license.search.SearchLicenseBatchUpdate;
import org.yht.exps.express.model.eCommerce.license.search.SearchLicenseList;

@Mapper
public interface LicenseMapper {

	List<License> selectLicenseEList(SearchLicenseList search);
	List<LicenseGoodsItem> selectLicenseGoodsItemOdList(SearchLicenseList search);
	List<LicenseGoodsItem> selectLicenseGoodsItemList(SearchLicenseList search);
	List<LicenseCommodityItem> selectLicenseCommodityItemOdList(SearchLicenseList search);
	List<LicenseCommodityItem> selectLicenseCommodityItemList(SearchLicenseList search);
	List<License> selectLicenseExcelList(SearchLicenseList search);
	
	License selectLicenseCustomer(SearchLicenseList search);
	License selectLicense(SearchLicenseList search);
	
	void insertLicense(License license);
	void insertLicenseGoodsItem(LicenseGoodsItem licenseGoodsItem);
	void insertLicenseCommodityItem(LicenseCommodityItem licenseCommodityItem);
	void insertLicenseOd(Map<String, Object> orderMap);
	
	void updateLicense(License license);
	void updateLicenseResultStatus(SearchLicenseBatchUpdate search);
	void updateLicenseGoodsItem(LicenseGoodsItem licenseGoodsItem);
	void updateLicenseCommodityItem(LicenseCommodityItem licenseCommodityItem);
	void updateOdLicenseType(License license);
	void updateLicenseSendResult(Map<String, Object> updateMap);
	
	void deleteLicense(License license);
	void deleteLicenseGoodsItem(LicenseGoodsItem licenseGoodsItem);
	void deleteLicenseCommodityItem(LicenseCommodityItem licenseCommodityItem);
	void deleteLicenseOd(Map<String, Object> orderMap);
	
	String selectLienseCount(SearchLicenseBatchUpdate search);
	List<Map<String, Object>> selectLicenseInfoList(SearchLicenseBatchUpdate search);
	List<Map<String, Object>> selectLicenseSendList(Map<String, Object> srchMap);
	Map<String, Object> selectBusinessApiKeyInfo(Map<String, Object> tmpMap);
	Map<String, Object> selectCustomerApiKeyInfo(Map<String, Object> tmpMap);

}
