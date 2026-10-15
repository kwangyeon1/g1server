package org.yht.exps.express.service.pda;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.pda.PdaCommonPacoMapper;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;
import org.yht.exps.express.model.bms.carryin.InPlanPkgList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PdaCommonPacoService {
	
	@Autowired
	PdaCommonPacoMapper pdaCommonPacoMapper;
	
	
	/**
	 * 박스번호 중복 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public InPlanPkgList checkDuplicatePkgNo(Map<String, Object> search) throws Exception {
		InPlanPkgList inPlanPkgList = new InPlanPkgList();
		
		List<InPlanPkg> list = pdaCommonPacoMapper.checkDuplicatePkgNo(search);
		inPlanPkgList.setInPlanPkgList(list);
		
		return inPlanPkgList;
	}
	
}
