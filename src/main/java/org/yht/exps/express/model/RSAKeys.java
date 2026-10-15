package org.yht.exps.express.model;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("RSAKeys")
public class RSAKeys {
	private String publicKeyModulus;
	private String publicKeyExponent;
}
