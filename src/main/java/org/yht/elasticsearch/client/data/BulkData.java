package org.yht.elasticsearch.client.data;

import java.util.Map;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("bulkData")
public class BulkData {

	public enum Type {
		CREATE, UPDATE, DELETE
	}
	
	private String indexName;
	private String id;
	private String routing;
	private Map<String, Object> jsonMap;
	private Type actionType;
}