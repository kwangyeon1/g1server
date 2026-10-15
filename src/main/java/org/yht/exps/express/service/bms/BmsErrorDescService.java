package org.yht.exps.express.service.bms;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.bms.BmsErrorDescMapper;
import org.yht.exps.express.model.bms.bmsErrorDesc.BmsErrorDesc;
import org.yht.exps.express.model.bms.bmsErrorDesc.BmsErrorDescList;
import org.yht.exps.express.model.bms.bmsErrorDesc.search.SearchBmsErrorDescList;
import org.yht.util.Util;

@Service
@Transactional
public class BmsErrorDescService {

	@Autowired
	BmsErrorDescMapper bmsErrorDescMapper;

	/**
	 * 오류통보 or 결과통보 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BmsErrorDescList selectBmsErrorDescList(SearchBmsErrorDescList search) throws Exception {
		BmsErrorDescList bmsErrorDescList = new BmsErrorDescList();
		List<BmsErrorDesc> list = null;

		if(search.getPage() != 0 && search.getPageSize() != 0) { search.setStartNum(search.getPageSize() * (search.getPage() - 1)); }
		if(Util.null2str(search.getChangeField()).equals("Y")) { Util.camelCaseToUnderscore(search); }

		/** BMS05(오류) 오류통보 */
		if("BMS05".equals(search.getLastPodStatus())) {
			list = bmsErrorDescMapper.selectBms05ErrorDescList(search);
		}
		/** BMS09(기각) 결과통보 */
		else if("BMS09".equals(search.getLastPodStatus())){
			list = bmsErrorDescMapper.selectBms09ErrorDescList(search);
		}
		bmsErrorDescList.setBmsErrorDescList(list);

		if(list.size() > 0) {
			bmsErrorDescList.setTotal(list.get(0).getTotal()); 
		} else {
			bmsErrorDescList.setTotal(0);
		}

		return bmsErrorDescList;
	}

}
