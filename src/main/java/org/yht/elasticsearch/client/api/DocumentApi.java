package org.yht.elasticsearch.client.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

/**
 * Document API 모음
 */
@Slf4j
public class DocumentApi {
	
	private RestHighLevelClient client;
	
	public DocumentApi(RestHighLevelClient client) {
		this.client = client;
	}
	
	/**
	 * 문서를 해당 인덱스에 생성한다.
	 * 문서는 XContentBuilder 로 기술
	 * @param indexName
	 * @param _id
	 * @param indexBuilder
	 * @return
	 */
	public IndexResponse createDocument(String indexName, String _id, XContentBuilder indexBuilder) {
		return createDocument(indexName, _id, null, indexBuilder);
	}
	
	/**
	 * 문서를 해당 인덱스에 생성한다.
	 * 문서는 XContentBuilder 로 기술
	 * routing 정보 추가
	 * @param indexName
	 * @param _id
	 * @param routing
	 * @param indexBuilder
	 * @return
	 */
	public IndexResponse createDocument(String indexName, String _id, String routing, XContentBuilder indexBuilder) {
		
		// 데이터 추가
		IndexRequest request;
		if ("".equals(Util.null2str(routing)))	request = new IndexRequest(indexName).id(_id);
		else									request = new IndexRequest(indexName).id(_id).routing(routing);
		request.source(indexBuilder);
		
		return _createDocument(request, indexName, _id);
	}
	
	/**
	 * 문서를 해당 인덱스에 생성한다.
	 * 문서는 String 로 기술
	 * @param indexName
	 * @param _id
	 * @param jsonString
	 * @return
	 */
	public IndexResponse createDocument(String indexName, String _id, String jsonString) {
		return createDocument(indexName, _id, null, jsonString);
	}
	
	/**
	 * 문서를 해당 인덱스에 생성한다.
	 * 문서는 String 로 기술
	 * routing 정보 추가
	 * @param indexName
	 * @param _id
	 * @param routing
	 * @param jsonString
	 * @return
	 */
	public IndexResponse createDocument(String indexName, String _id, String routing, String jsonString) {
		
		// 데이터 추가
		IndexRequest request;
		if ("".equals(Util.null2str(routing)))	request = new IndexRequest(indexName).id(_id);
		else									request = new IndexRequest(indexName).id(_id).routing(routing);
		
		request.source(jsonString, XContentType.JSON);
		
		return _createDocument(request, indexName, _id);
	}
	
	/**
	 * 문서를 해당 인덱스에 생성한다.
	 * 문서는 Map<String, Object> 로 기술
	 * @param indexName
	 * @param _id
	 * @param jsonMap
	 * @return
	 */
	public IndexResponse createDocument(String indexName, String _id, Map<String, Object> jsonMap) {
		return createDocument(indexName, _id, null, jsonMap);
	}
	
	/**
	 * 문서를 해당 인덱스에 생성한다.
	 * 문서는 Map<String, Object> 로 기술
	 * routing 정보 추가
	 * @param indexName
	 * @param _id
	 * @param routing
	 * @param jsonMap
	 * @return
	 */
	public IndexResponse createDocument(String indexName, String _id, String routing, Map<String, Object> jsonMap) {
		
		// 데이터 추가
		IndexRequest request;
		if ("".equals(Util.null2str(routing)))	request = new IndexRequest(indexName).id(_id);
		else									request = new IndexRequest(indexName).id(_id).routing(routing);
		
		request.source(jsonMap);
		
		return _createDocument(request, indexName, _id);
	}
	
	/**
	 * 문서를 해당 인덱스에 생성
	 * @param request
	 * @param indexName
	 * @param _id
	 * @return
	 */
	private IndexResponse _createDocument(IndexRequest request, String indexName, String _id) {
		request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
		// 결과 조회
		IndexResponse response = null;
		try {
			response = client.index(request, RequestOptions.DEFAULT);
			log.info(response.status() + " in " +indexName + " :: created id=" + _id);
		} catch (ElasticsearchException | IOException e) {
			log.error(e.getMessage());
		}
		
		return response;
	}
	
	/**
	 * 문서를 조회하고 해당 문서의 source를 반환
	 * @param indexName
	 * @param _id
	 * @return
	 */
	public GetResponse getDocument(String indexName, String _id) {
		GetRequest request = new GetRequest(indexName, _id);
		return _getDocument(request);
	}
	
	/**
	 * 문서를 조회하고 해당 문서의 source를 반환
	 * @param indexName
	 * @param _id
	 * @params _routing
	 * @return
	 */
	public GetResponse getDocument(String indexName, String _id, String _routing) {
		GetRequest request = new GetRequest(indexName, _id);
		request.routing(_routing);
		return _getDocument(request);
	}
	
	/**
	 * 문서를 조회하고 해당 문서의 특정 필드만을 반환
	 * 이 필드는 매핑에서 저장하도록 설정이 되어 있어야만 가능하다.
	 * @param indexName
	 * @param fieldName
	 * @param _id
	 * @return
	 */
	public GetResponse getDocument(String indexName, String[] fieldName, String _id) {
		GetRequest request = new GetRequest(indexName, _id);
		//fieldName만을 결과로 반환
		request.storedFields(fieldName); 
		return _getDocument(request);
	}
	
	/**
	 * 문서를 조회하고 해당 문서의 특정 필드만을 반환
	 * 이 필드는 매핑에서 저장하도록 설정이 되어 있어야만 가능하다.
	 * @param indexName
	 * @param fieldName
	 * @param _id
	 * @param _routing
	 * @return
	 */
	public GetResponse getDocument(String indexName, String[] fieldName, String _id, String _routing) {
		GetRequest request = new GetRequest(indexName, _id);
		request.routing(_routing);
		//fieldName만을 결과로 반환
		request.storedFields(fieldName); 
		return _getDocument(request);
	}

	/**
	 * 문서를 조회하고 해당 문서의 source를 반환
	 * source에 포함할 필드와 포함하지 않을 필드는 인수로 넘겨 받은 값에 따라 정해진다.
	 * @param indexName
	 * @param _id
	 * @param includeField
	 * @param excludeField
	 * @return
	 */
	public GetResponse getDocument(String indexName, String _id, String[] includeField, String[] excludeField) {
		GetRequest request = new GetRequest(indexName, _id);
		//결과로 반환하는 source에 포함할 필드와 포함하지 않을 필드를 설정
		FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includeField, excludeField);
		request.fetchSourceContext(fetchSourceContext); 
		return _getDocument(request);
	}
	
	/**
	 * 문서를 조회하고 해당 문서의 source를 반환
	 * source에 포함할 필드와 포함하지 않을 필드는 인수로 넘겨 받은 값에 따라 정해진다.
	 * @param indexName
	 * @param _id
	 * @param _routing
	 * @param includeField
	 * @param excludeField
	 * @return
	 */
	public GetResponse getDocument(String indexName, String _id, String _routing, String[] includeField, String[] excludeField) {
		GetRequest request = new GetRequest(indexName, _id);
		request.routing(_routing);
		//결과로 반환하는 source에 포함할 필드와 포함하지 않을 필드를 설정
		FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includeField, excludeField);
		request.fetchSourceContext(fetchSourceContext); 
		return _getDocument(request);
	}
	
	/**
	 * 문서를 조회한다.
	 * @param request
	 * @return
	 */
	private GetResponse _getDocument(GetRequest request) {
		GetResponse response = null;
		try {
			response = client.get(request, RequestOptions.DEFAULT);
			if(!response.isExists()) {
				log.info("문서를 찾을 수 없습니다.");
				return null;
			}
		} catch (ElasticsearchException  e) {
			if (e.status() == RestStatus.NOT_FOUND) {
				log.error("인덱스를 찾을 수 없습니다.", e);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return response;
	}
	
	/**
	 * 복수개의 문서를 조회한다.
	 * @param indexName
	 * @param _id
	 * @return
	 */
	public MultiGetResponse getMultiDocument(String indexName, String... _id) {
		MultiGetRequest request = new MultiGetRequest();
		for(String id : _id) {
			request.add(new MultiGetRequest.Item(indexName, id));
		}
		MultiGetResponse response = null;
		try {
			response = client.mget(request, RequestOptions.DEFAULT);
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return response;
	}
	
	/**
	 * 복수개의 문서를 조회하고 해당 문서의 특정 필드만을 반환
	 * 이 필드는 매핑에서 저장하도록 설정이 되어 있어야만 가능하다.
	 * @param indexName
	 * @param fieldName
	 * @param _id
	 * @return
	 */
	public MultiGetResponse getMultiDocument(String indexName, String[] fieldName, String... _id) {
		MultiGetRequest request = new MultiGetRequest();
		for(String id : _id) {
			request.add(new MultiGetRequest.Item(indexName, id).storedFields(fieldName));
		}
		MultiGetResponse response = null;
		try {
			response = client.mget(request, RequestOptions.DEFAULT);
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return response;
	}

	/**
	 * 복수개의 문서를 조회하고 해당 문서의 source를 반환
	 * source에 포함할 필드와 포함하지 않을 필드는 인수로 넘겨 받은 값에 따라 정해진다.
	 * @param indexName
	 * @param includeField
	 * @param excludeField
	 * @param _id
	 * @return
	 */
	public MultiGetResponse getMultiDocument(String indexName, String[] includeField, String[] excludeField, String... _id) {
		MultiGetRequest request = new MultiGetRequest();
		FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includeField, excludeField);
		for(String id : _id) {
			request.add(new MultiGetRequest.Item(indexName, id).fetchSourceContext(fetchSourceContext));
		}
		MultiGetResponse response = null;
		try {
			response = client.mget(request, RequestOptions.DEFAULT);
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return response;
	}
	
	
	/**
	 * 문서가 해당 인덱스에 존재하는지 여부를 반환한다.
	 * @param indexName
	 * @param _id
	 * @return
	 */
	public Boolean existDocument(String indexName, String _id) {
		GetRequest request = new GetRequest(indexName, _id);
		
		request.fetchSourceContext(new FetchSourceContext(false));
		request.storedFields("_none_");
		boolean exists = false; 
		try {
			exists = client.exists(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error(Util.getPrintStacTraceString(e));
		}
		return exists;
		
	}
	
	/**
	 * 문서를 업데이트 한다.
	 * @param indexName
	 * @param _id
	 * @param updateField
	 * @param value
	 * @return
	 */
	public UpdateResponse updateDocumentWithScript(String indexName, String _id, String updateField, String value) {
		return updateDocumentWithScript(indexName, _id, null, updateField, value);
	}
	
	/**
	 * 문서를 업데이트 한다.
	 * routing 정보 추가
	 * @param indexName
	 * @param _id
	 * @param routing
	 * @param updateField
	 * @param value
	 * @return
	 */
	public UpdateResponse updateDocumentWithScript(String indexName, String _id, String routing, String updateField, String value) {
		UpdateRequest request = new UpdateRequest(indexName, _id);
		if (!"".equals(Util.null2str(routing)))	request.routing(routing);
		
		Map<String, Object> parameters =  Collections.singletonMap(updateField, value); 

		Script inline = new Script(ScriptType.INLINE, "painless",
		        "ctx._source."+updateField+" = params."+updateField, parameters);  
		request.script(inline);
		
		return _updateDocument(request, indexName, _id);
	}
	
	/**
	 * 문서를 업데이트 한다.
	 * 업데이트 내용을 json형태의 string으로 기술한다.
	 * @param indexName
	 * @param _id
	 * @param jsonString
	 * @return
	 */
	public UpdateResponse updateDocument(String indexName, String _id, String jsonString) {
		return updateDocument(indexName, _id, null, jsonString);
	}
	
	/**
	 * 문서를 업데이트 한다.
	 * 업데이트 내용을 json형태의 string으로 기술한다.
	 * routing 정보 추가
	 * @param indexName
	 * @param _id
	 * @parma routing
	 * @param jsonString
	 * @return
	 */
	public UpdateResponse updateDocument(String indexName, String _id, String routing, String jsonString) {
		UpdateRequest request = new UpdateRequest(indexName, _id);
		if (!"".equals(Util.null2str(routing)))	request.routing(routing);
		
		request.doc(jsonString, XContentType.JSON);
		
		return _updateDocument(request, indexName, _id);
	}

	/**
	 * 문서를 업데이트 한다.
	 * 업데이트 내용을 Map<String, Object>으로 기술한다.
	 * @param indexName
	 * @param _id
	 * @param jsonMap
	 * @return
	 */
	public UpdateResponse updateDocument(String indexName, String _id, Map<String, Object> jsonMap) {
		return updateDocument(indexName, _id, null, jsonMap);
	}
	
	/**
	 * 문서를 업데이트 한다.
	 * 업데이트 내용을 Map<String, Object>으로 기술한다.
	 * routing 정보 추가
	 * @param indexName
	 * @param _id
	 * @param routing
	 * @param jsonMap
	 * @return
	 */
	public UpdateResponse updateDocument(String indexName, String _id, String routing, Map<String, Object> jsonMap) {
		UpdateRequest request = new UpdateRequest(indexName, _id);
		if (!"".equals(Util.null2str(routing)))	request.routing(routing);
		
		request.doc(jsonMap, XContentType.JSON);
		
		return _updateDocument(request, indexName, _id);
	}
	
	/**
	 * 문서를 업데이트 한다.
	 * 업데이트 내용을 XContentBuilder으로 기술한다.
	 * @param indexName
	 * @param _id
	 * @param builder
	 * @return
	 */
	public UpdateResponse updateDocument(String indexName, String _id, XContentBuilder builder) {
		return updateDocument(indexName, _id, null, builder);
	}
	
	/**
	 * 문서를 업데이트 한다.
	 * 업데이트 내용을 XContentBuilder으로 기술한다.
	 * routing 정보 추가
	 * @param indexName
	 * @param _id
	 * @param routing
	 * @param builder
	 * @return
	 */
	public UpdateResponse updateDocument(String indexName, String _id, String routing, XContentBuilder builder) {
		UpdateRequest request = new UpdateRequest(indexName, _id);
		request.doc(builder, XContentType.JSON);
		
		return _updateDocument(request, indexName, _id);
	}
	
	/**
	 * 문서의 내용을 업데이트 한다.
	 * @param request
	 * @param indexName
	 * @param _id
	 * @return
	 */
	private UpdateResponse _updateDocument(UpdateRequest request, String indexName, String _id) {
		request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
		UpdateResponse response = null;
		try {
			response = client.update(request, RequestOptions.DEFAULT);
			log.info(response.status() + " in " +indexName + " :: updated id=" + _id);
		} catch (ElasticsearchException | IOException e) {
			if(((ElasticsearchException) e).status().equals(RestStatus.CONFLICT)) {
				log.error("문서 업데이트 실패", e);
			}
			log.error(e.getMessage());
		}
		
		return response;
	}
	
	/**
	 * 문서를 삭제한다.
	 * @param indexName
	 * @param _id
	 * @return
	 */
	public DeleteResponse deleteDocument(String indexName, String _id) {
		return deleteDocument(indexName, _id, null);
	}
	
	/**
	 * 문서를 삭제한다.
	 * routing 정보 추가
	 * @param indexName
	 * @param _id
	 * @param routing
	 * @return
	 */
	public DeleteResponse deleteDocument(String indexName, String _id, String routing) {
		DeleteRequest request = new DeleteRequest(indexName, _id);
		request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
		if (!"".equals(Util.null2str(routing)))	request.routing(routing);
		
		DeleteResponse response = null;
		try {
			response = client.delete(request, RequestOptions.DEFAULT);
			log.info(response.status() + " in " +indexName + " :: deleted id=" + _id);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return response;
	}
	
	public Object getBulkItem() {
		BulkRequest request = new BulkRequest(); 
		Object[] item = {request, client};
		return item;
	}
	
	/**
	 * BulkProcessor를 반환한다.
	 * flush는 기본 1000개로 정해진다.
	 * @return
	 */
	public BulkProcessor getBulkProcessor() {
		return _getBulkProcessor(1000); 
	}
	
	/**
	 * ArrayList<BulkData>를 입력받아 bulk request를 수행한다.
	 * @param bulkList
	 */
	public BulkResponse bulkDocument(ArrayList<BulkData> bulkList) {
		
		BulkRequest request = new BulkRequest();
		
		
		for(BulkData data : bulkList) {
			if(data.getActionType().equals(BulkData.Type.CREATE)) {
				if ("".equals(Util.null2str(data.getRouting()))) 
					request.add(new IndexRequest(data.getIndexName()).source(data.getJsonMap()));
				else
					request.add(new IndexRequest(data.getIndexName()).source(data.getJsonMap()).routing(data.getRouting()));
				
			} else if(data.getActionType().equals(BulkData.Type.UPDATE)) {
				if ("".equals(Util.null2str(data.getRouting())))
					request.add(new UpdateRequest(data.getIndexName(), data.getId()).doc(data.getJsonMap()));
				else 
					request.add(new UpdateRequest(data.getIndexName(), data.getId()).doc(data.getJsonMap()).routing(data.getRouting()));
				
			} else if(data.getActionType().equals(BulkData.Type.DELETE)) {
				if ("".equals(Util.null2str(data.getRouting())))
					request.add(new DeleteRequest(data.getIndexName(), data.getId()));
				else
					request.add(new DeleteRequest(data.getIndexName(), data.getId()).routing(data.getRouting()));
			}
		}
		
		BulkResponse response = null;
		try {
			response = client.bulk(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		for (BulkItemResponse bulkItemResponse : response) { 
			if (bulkItemResponse.isFailed()) { 
		        BulkItemResponse.Failure failure =bulkItemResponse.getFailure(); 
		        log.info(failure.getIndex() +" - " + failure.getType() + " - "+ failure.getId() + " / " + failure.getMessage());
		    }
		}
		log.info(response.getItems().toString());
		log.info(response.getTook().toString());
		
		return response;
	}

	
	/**
	 * BulkProcessor를 반환한다.
	 * @param bulkActions flush 할 건수
	 * @return
	 */
	public BulkProcessor getBulkProcessor(int bulkActions) {
		return _getBulkProcessor(bulkActions); 
	}
	
	private BulkProcessor _getBulkProcessor(int bulkActions ) {
		BulkProcessor bulkProcessor = BulkProcessor.builder(
				(request, bulkListener) ->
				client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),new BulkProcessor.Listener() {
					int count = 0;
					
					@Override
					public void beforeBulk(long l, BulkRequest bulkRequest) {
						bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
						count = count + bulkRequest.numberOfActions();
						log.info("Uploaded " + count + " so far");
					}
					@Override
					public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
						if (bulkResponse.hasFailures()) {
							for (BulkItemResponse bulkItemResponse : bulkResponse) {
								if (bulkItemResponse.isFailed()) {
									BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
									log.info("Error " + failure.toString());
								}
							}
						}
					}
					@Override
					public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
						log.info("Errors " + throwable.toString());
					}
				}, "bulk-processor")
				.setBulkActions(bulkActions)
				.setConcurrentRequests(0)
				.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB))
				.setFlushInterval(TimeValue.timeValueSeconds(5L))
				.setBackoffPolicy(BackoffPolicy
				        .constantBackoff(TimeValue.timeValueSeconds(1L), 3))
				.build();
		return bulkProcessor; 
	}
	
	
	public Boolean bulkDocumentWithBulkProcessor(ArrayList<BulkData> bulkList) {
		return _bulkDocumentWithBulkProcessor(bulkList, 1000);
	}
	
	public Boolean bulkDocumentWithBulkProcessor(ArrayList<BulkData> bulkList, int bulkActions) {
		return _bulkDocumentWithBulkProcessor(bulkList, bulkActions);
	}
	
	private Boolean _bulkDocumentWithBulkProcessor(ArrayList<BulkData> bulkList, int bulkActions) {
		boolean terminated = false ;
		BulkProcessor bulkProcessor = _getBulkProcessor(bulkActions);
		
		for(BulkData data : bulkList) {
			if(data.getActionType().equals(BulkData.Type.CREATE)) {
				if ("".equals(Util.null2str(data.getRouting()))) 
					bulkProcessor.add(new IndexRequest(data.getIndexName()).id(data.getId()).source(data.getJsonMap()));
				else
					bulkProcessor.add(new IndexRequest(data.getIndexName()).id(data.getId()).source(data.getJsonMap()).routing(data.getRouting()));
				
			} else if(data.getActionType().equals(BulkData.Type.UPDATE)) {
				if ("".equals(Util.null2str(data.getRouting())))
					bulkProcessor.add(new UpdateRequest(data.getIndexName(), data.getId()).doc(data.getJsonMap()));
				else 
					bulkProcessor.add(new UpdateRequest(data.getIndexName(), data.getId()).doc(data.getJsonMap()).routing(data.getRouting()));
				
			} else if(data.getActionType().equals(BulkData.Type.DELETE)) {
				if ("".equals(Util.null2str(data.getRouting())))
					bulkProcessor.add(new DeleteRequest(data.getIndexName(), data.getId()));
				else
					bulkProcessor.add(new DeleteRequest(data.getIndexName(), data.getId()).routing(data.getRouting()));
			}
		}
		try {
			terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);
			log.info("BulkProcessor Finished...." + terminated);
			bulkProcessor.close();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		} 
		return terminated;
	}
}
