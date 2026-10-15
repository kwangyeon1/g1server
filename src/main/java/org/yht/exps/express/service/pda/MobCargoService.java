package org.yht.exps.express.service.pda;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.pda.MobCargoMapper;
import org.yht.exps.express.model.mobile.bms.MobCargo;
import org.yht.exps.express.model.mobile.bms.MobCargoList;
import org.yht.exps.express.model.mobile.bms.search.SearchMobCargo;

@Service
@Transactional
public class MobCargoService {

	@Autowired
	MobCargoMapper mobCargoMapper;
	@Autowired
    CommonMapper commonMapper;
		
	/**
	 * 화물조회 - 화물 상세 조회
	 * 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MobCargo selectCargoDetail(SearchMobCargo search) throws Exception {
		MobCargo order = null;
		
		if("IN".equals(search.getDGbn())) {
			order = mobCargoMapper.selectCargoDetailByCarryIn(search); // 반입이 선택되었을때
		} else if("IN_PLAN".equals(search.getDGbn())) {
			order = mobCargoMapper.selectCargoDetailByInPlan(search);  // 반입예정이 선택되었을때
		} else if("ORDER".equals(search.getDGbn())) {
			order = mobCargoMapper.selectCargoDetailByOrder(search);   // 오더가 선택되었을때
		}
		
		return order;
	}
	
	/**
	 * 화물조회 - BL 중복체크
	 * 
	 * @param map	
	 * @return
	 * @throws Exception
	 */
	public MobCargoList checkCargoListByBlNo(SearchMobCargo search) {
		MobCargoList mobCargoList = new MobCargoList();
		List<MobCargo> list = mobCargoMapper.checkCargoListByBlNo(search);
		mobCargoList.setList(list);
		if (list.size() > 0) {
			mobCargoList.setTotal(list.get(0).getTotal());
		} else {
			mobCargoList.setTotal(0);
		}
		return mobCargoList;
	}
}
