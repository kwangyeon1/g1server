package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CustomsInfoJson{
	@JsonAlias({"CustomNm", "CustomNm"})
	private String CustomNm;
	
}