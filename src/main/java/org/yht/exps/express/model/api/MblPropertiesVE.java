package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class MblPropertiesVE {
	@JsonAlias({"ETA", "eta"})
	private String eta;
	@JsonAlias({"ETD", "etd"})
	private String etd;
}
