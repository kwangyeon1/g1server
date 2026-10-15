package org.yht.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.yht.exps.express.model.properties.ElasticSearchProperties;

@Configuration
@ConfigurationProperties(value = "elastic-client", ignoreUnknownFields = false)
public class ElasticsearchConfig {
	
	@Autowired
	ElasticSearchProperties esProperties;
	
    @Bean(destroyMethod = "close")
    public RestHighLevelClient elasticsearchClient() {
    	final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    	credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esProperties.getUser(), esProperties.getPass()));

        List<HttpHost> hostList = new ArrayList<>();
        for(String host : esProperties.getHosts()) {
            hostList.add(new HttpHost(host, esProperties.getPort(), "http"));
        }
        
        RestClientBuilder builder = RestClient.builder(hostList.toArray(new HttpHost[hostList.size()]))
        	    .setHttpClientConfigCallback(new HttpClientConfigCallback() {
        	        @Override
        	        public HttpAsyncClientBuilder customizeHttpClient(
        	                HttpAsyncClientBuilder httpClientBuilder) {
        	            return httpClientBuilder
        	                .setDefaultCredentialsProvider(credentialsProvider);
        	        }
        	    });
        
        return new RestHighLevelClient(builder);
    }
    
    @Bean
    ElasticsearchRestTemplate elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }
}
