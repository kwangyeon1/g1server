package org.yht.exps.express.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "elasticsearch")
@Data
public class ElasticSearchProperties {
	String[] hosts;
	int port;
	String user;
	String pass;
}
