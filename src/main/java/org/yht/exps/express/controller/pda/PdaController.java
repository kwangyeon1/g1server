package org.yht.exps.express.controller.pda;


import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yht.exps.express.service.pda.PdaService;

import io.swagger.annotations.Api;

@RestController
@Api(tags = "pda")
@RequestMapping("/{entCode}/ecm/pda")
public class PdaController {
	
	@Resource(name = "pdaService")
	PdaService pdaService;
	
	
	
}
