package org.yht.exps.express.mapper.sms;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EnterpriseMapper {
	Map<String, Object> getBusinessClosingInfo(Map<String, Object> search);
	String getEntBusinessPrintName(String entBusinessCode);
}
