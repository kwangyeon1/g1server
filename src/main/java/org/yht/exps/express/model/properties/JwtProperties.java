package org.yht.exps.express.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
	private String secretKey;
	private long validityInMs;
	private long validityInMsRefresh;
}