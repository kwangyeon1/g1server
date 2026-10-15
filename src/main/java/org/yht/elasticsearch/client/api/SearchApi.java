package org.yht.elasticsearch.client.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.yht.elasticsearch.client.data.HighlightData;
import org.yht.elasticsearch.client.data.SortData;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

/**
 * Search API 모음
 */
@Slf4j
public class SearchApi {

	private RestHighLevelClient client;
	
	public SearchApi(RestHighLevelClient client) {
		this.client = client;
	}
	
	public SearchResponse search(QueryBuilder qBuilder) {
		return _search(qBuilder, 0, 10, 0, null, null, null, null, null, null);
	}
	
	public SearchResponse search(QueryBuilder qBuilder, boolean fetchSource) {
		return _search(qBuilder, 0, 10, 0, null, null, null, fetchSource, null);
	}
	
	public SearchResponse search(QueryBuilder qBuilder, List<SortData> sortList) {
		return _search(qBuilder, 0, 10, 0, null, sortList, null, null, null, null);
	}
	
	public SearchResponse search(QueryBuilder qBuilder, String[] indices) {
		return _search(qBuilder, 0, 10, 0, indices, null, null, null, null, null);
	}
	
	public SearchResponse search(QueryBuilder qBuilder, String[] indices, List<SortData> sortList) {
		return _search(qBuilder, 0, 10, 0, indices, sortList, null, null, null, null);
	}
	
	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits) {
		return _search(qBuilder, startNum, size, maxTotalHits, null, null, null, null, null, null);
	}
	
	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, boolean fetchSource) {
		return _search(qBuilder, startNum, size, maxTotalHits, null, null, null, fetchSource, null);
	}
	
	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, AggregationBuilder aggregation) {
		return _search(qBuilder, startNum, size, maxTotalHits, null, null, null, null, null, aggregation);
	}
	
	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, List<SortData> sortList) {
		return _search(qBuilder, startNum, size, maxTotalHits, null, sortList, null, null, null, null);
	}

	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, List<SortData> sortList, AggregationBuilder aggregation) {
		return _search(qBuilder, startNum, size, maxTotalHits, null, sortList, null, null, null, aggregation);
	}

	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, String[] indices) {
		return _search(qBuilder, startNum, size, maxTotalHits, indices, null, null, null, null, null);
	}	
	
	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, String[] indices, boolean fetchSource) {
		return _search(qBuilder, startNum, size, maxTotalHits, indices, null, null, fetchSource, null);
	}
	
	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, String[] indices, List<SortData> sortList) {
		return _search(qBuilder, startNum, size, maxTotalHits, indices, sortList, null, null, null, null);
	}
	
	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, String[] indices, List<SortData> sortList, AggregationBuilder aggregation) {
		return _search(qBuilder, startNum, size, maxTotalHits, indices, sortList, null, null, null, aggregation);
	}
	
	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, String[] indices, List<SortData> sortList, boolean fetchSource) {
		return _search(qBuilder, startNum, size, maxTotalHits, indices, sortList, null, fetchSource, null);
	}

	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, String[] indices, List<SortData> sortList, List<HighlightData> highlightList) {
		return _search(qBuilder, startNum, size, maxTotalHits, indices, sortList, highlightList, null, null, null);
	}

	public SearchResponse search(QueryBuilder qBuilder,int startNum, int size, int maxTotalHits, String[] indices, List<SortData> sortList, List<HighlightData> highlightList, AggregationBuilder aggregation) {
		return _search(qBuilder, startNum, size, maxTotalHits, indices, sortList, highlightList, null, null, aggregation);
	}
	
	private SearchResponse _search(QueryBuilder qBuilder, int startNum, int size, int maxTotalHits, String[] indices, List<SortData> sortList, List<HighlightData> highlightList, 
			boolean fetchSource, AggregationBuilder aggregation) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		searchSourceBuilder.fetchSource(fetchSource);
		if(aggregation != null)
			searchSourceBuilder.aggregation(aggregation);
		return _searchExcution(searchSourceBuilder, qBuilder, startNum, size, maxTotalHits, indices, sortList, highlightList);
	}

	private SearchResponse _search(QueryBuilder qBuilder, int startNum, int size, int maxTotalHits, String[] indices, List<SortData> sortList, List<HighlightData> highlightList, 
			String[] includeFields, String[] excludeFields, AggregationBuilder aggregation) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		if(includeFields != null && excludeFields!= null)
			searchSourceBuilder.fetchSource(includeFields, excludeFields);
		if(aggregation != null)
			searchSourceBuilder.aggregation(aggregation);
		return _searchExcution(searchSourceBuilder, qBuilder, startNum, size, maxTotalHits, indices, sortList, highlightList);
	}
	
	private SearchResponse _searchExcution(SearchSourceBuilder searchSourceBuilder, QueryBuilder qBuilder, int startNum, int size, int maxTotalHits, String[] indices, 
			List<SortData> sortList, List<HighlightData> highlightList) {
		SearchRequest request = new SearchRequest();
		if (maxTotalHits > 0)	searchSourceBuilder.trackTotalHitsUpTo(maxTotalHits);
		searchSourceBuilder.query(qBuilder); 
		searchSourceBuilder.from(startNum);
		searchSourceBuilder.size(size);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		if(highlightList != null) {
			HighlightBuilder highlightBuilder = new HighlightBuilder(); 
			for(HighlightData data : highlightList) {
				HighlightBuilder.Field highlightField = new HighlightBuilder.Field(data.getField());
				if(data.getType() != null)
					highlightField.highlighterType(data.getType());
				else
					highlightField.highlighterType("unified");
				if(data.getPreTag() != null && data.getPostTag() != null) {
					highlightField.preTags(data.getPreTag());
					highlightField.postTags(data.getPostTag());
				}
				highlightBuilder.field(highlightField);
				
				searchSourceBuilder.highlighter(highlightBuilder);
			}
		}
		if(sortList != null) {
			for(SortData data : sortList) {
				if("score".equals(data.getField()))
					searchSourceBuilder.sort(new ScoreSortBuilder().order(data.getType()));
				else if("id".equals(data.getField()))
					searchSourceBuilder.sort(new FieldSortBuilder("_uid").order(data.getType()));
				else 
					searchSourceBuilder.sort(new FieldSortBuilder(data.getField()).order(data.getType()));
			}
		}
		if(indices != null)
			request.indices(indices);
		request.source(searchSourceBuilder); 
		SearchResponse response = null;
		try {
			log.info("--Query: " + searchSourceBuilder.toString());
			log.info("--Indices: " + Arrays.toString(request.indices()));
			response = client.search(request, RequestOptions.DEFAULT);
			log.info("--- Response Time(" + response.status() + "): " + response.getTook() + " milliseconds");
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return response;
	}

	public SearchResponse searchScroll(QueryBuilder qBuilder, int size, String[] indices, String scrollId) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		return _searchScroll(searchSourceBuilder, qBuilder, size, indices, scrollId, null, null);
	}

	public SearchResponse searchScroll(QueryBuilder qBuilder, int size, String[] indices, String scrollId, List<SortData> sortList) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		return _searchScroll(searchSourceBuilder, qBuilder, size, indices, scrollId, sortList, null);
	}
	
	public SearchResponse searchScroll(QueryBuilder qBuilder, int size, String[] indices, String scrollId, List<SortData> sortList, AggregationBuilder aggregation) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		if(aggregation != null)
			searchSourceBuilder.aggregation(aggregation);
		return _searchScroll(searchSourceBuilder, qBuilder, size, indices, scrollId, sortList, null);
	}

	public SearchResponse searchScroll(QueryBuilder qBuilder, int size, String[] indices, String scrollId, List<SortData> sortList, List<HighlightData> highlightList) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		return _searchScroll(searchSourceBuilder, qBuilder, size, indices, scrollId, sortList, highlightList);
	}

	public SearchResponse searchScroll(QueryBuilder qBuilder, int size, String[] indices, String scrollId, boolean fetchSource) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		searchSourceBuilder.fetchSource(fetchSource);
		return _searchScroll(searchSourceBuilder, qBuilder, size, indices, scrollId, null, null);
	}
	
	public SearchResponse searchScroll(QueryBuilder qBuilder, int size, String[] indices, String scrollId, boolean fetchSource, List<SortData> sortList, List<HighlightData> highlightList, AggregationBuilder aggregation) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		searchSourceBuilder.fetchSource(fetchSource);
		if(aggregation != null)
			searchSourceBuilder.aggregation(aggregation);
		return _searchScroll(searchSourceBuilder, qBuilder, size, indices, scrollId, sortList, highlightList);
	}
	
	private SearchResponse _searchScroll(SearchSourceBuilder searchSourceBuilder, QueryBuilder qBuilder, int size, String[] indices, 
			String scrollId, List<SortData> sortList, List<HighlightData> highlightList) {
		final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
		SearchRequest searchRequest = new SearchRequest(indices);
		searchSourceBuilder.query(qBuilder);
		searchSourceBuilder.size(size); 
		searchRequest.source(searchSourceBuilder);
		searchRequest.scroll(scroll); 
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		if(highlightList != null) {
			HighlightBuilder highlightBuilder = new HighlightBuilder(); 
			for(HighlightData data : highlightList) {
				HighlightBuilder.Field highlightField = new HighlightBuilder.Field(data.getField());
				if(data.getType() != null)
					highlightField.highlighterType(data.getType());
				else
					highlightField.highlighterType("unified");
				if(data.getPreTag() != null && data.getPostTag() != null) {
					highlightField.preTags(data.getPreTag());
					highlightField.postTags(data.getPostTag());
				}
				highlightBuilder.field(highlightField);
				
				searchSourceBuilder.highlighter(highlightBuilder);
			}
		}
		if(sortList != null) {
			for(SortData data : sortList) {
				if("score".equals(data.getField()))
					searchSourceBuilder.sort(new ScoreSortBuilder().order(data.getType()));
				else if("id".equals(data.getField()))
					searchSourceBuilder.sort(new FieldSortBuilder("_uid").order(data.getType()));
				else 
					searchSourceBuilder.sort(new FieldSortBuilder(data.getField()).order(data.getType()));
			}
		}
		
		SearchResponse response = null;
		try {
			log.info("Query: " + searchSourceBuilder.toString());
			log.info("--Indices: " + Arrays.toString(searchRequest.indices()));
			if (scrollId == null || "".equals(scrollId)) {
				response = client.search(searchRequest, RequestOptions.DEFAULT);
			} else {
				SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId).scroll(scroll);
				response = client.scroll(searchScrollRequest, RequestOptions.DEFAULT);				
			}
			log.info("--- Response Time(" + response.status() + "): " + response.getTook() + " milliseconds");
			
		} catch (IOException e) {
			log.error(Util.getPrintStacTraceString(e));
		}
		
		return response;
	}
	
	/**
	 * Close Scroll API
	 * @param scrollId
	 * @return
	 */
	public ClearScrollResponse clearScollRequest(String scrollId) {
		ClearScrollRequest request = new ClearScrollRequest();
		request.addScrollId(scrollId);
		
		ClearScrollResponse response = null;
		try {
			response = client.clearScroll(request, RequestOptions.DEFAULT);
			String logVal = "Close Scroll API: " + scrollId;
			log.info(logVal);
			log.info("--- Response Status: " + response.status());
		} catch (IOException e) {
			log.error(Util.getPrintStacTraceString(e));
		}
		
		return response;
	}
	
	/**
	 * RestHighLevelClient 객체를 반환
	 * @return
	 */
	public RestHighLevelClient getClient() {
		return client;
	}
	
	/**
	 * Search Template으로 검색한다.
	 * @param script
	 * @param scriptParams
	 * @param indices
	 * @return
	 */
	public SearchResponse searchTemplate(String script, Map<String, Object> scriptParams, String... indices) {
		SearchTemplateRequest request = new SearchTemplateRequest();
		request.setRequest(new SearchRequest(indices)); 

		request.setScriptType(ScriptType.INLINE);
		request.setScript( script);

		request.setScriptParams(scriptParams);
		
		SearchTemplateResponse searchTemplateResponse;
		SearchResponse response = null;
		try {
			searchTemplateResponse = client.searchTemplate(request, RequestOptions.DEFAULT);
			response = searchTemplateResponse.getResponse();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return response;
	}
}