package org.yht.exps.express.service.pda;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.pda.BmsMobBwisMapper;
import org.yht.exps.express.model.mobile.bms.MobBwis;
import org.yht.exps.express.model.mobile.bms.MobBwisSaveResult;
import org.yht.exps.express.model.mobile.bms.search.SearchMobBwis;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BmsMobBwisService {
	
	@Autowired
	BmsMobBwisMapper bmsMobBwisMapper;
	
	/**
	 * 검사대상확인 - 검사대상 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobBwis selectInspectResult(SearchMobBwis search) throws Exception {
		return bmsMobBwisMapper.selectInspectResult(search);
	}
	
	/**
	 * 검사대상확인 - 검사 시작 업데이트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MobBwisSaveResult updateSeInspectStart(SearchMobBwis search) throws Exception {
		MobBwisSaveResult result = new MobBwisSaveResult();
		if(bmsMobBwisMapper.updateSeInspectStart(search) > 0) {
			result.setResultCode("S");
		}else {
			result.setResultCode("F");
		}
		return result;
	}
}
