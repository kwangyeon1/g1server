package org.yht.exps.express.service.pda;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.pda.PdaMapper;

@Service
@Transactional
public class PdaService {

	@Autowired
	PdaMapper pdaMapper;
		

}
