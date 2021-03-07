package com.guoli.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.guoli.search.pojo.Content;
import com.guoli.search.service.ContentService;
import com.guoli.search.util.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 * @author guoli
 * @data 2021-03-06 22:58
 */
@Service
public class ContentServiceImpl implements ContentService {
    private final HtmlParseUtil htmlParseUtil;

    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public ContentServiceImpl(HtmlParseUtil htmlParseUtil, RestHighLevelClient restHighLevelClient) {
        this.htmlParseUtil = htmlParseUtil;
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public String parseDataToEs(String keyword) {
        try {
            // 获取数据
            List<Content> contents = htmlParseUtil.parseJd(keyword);

            // 判断索引是否存在
            GetIndexRequest existsRequest = new GetIndexRequest("jd_commodity");
            boolean existsResponse = restHighLevelClient.indices().exists(existsRequest, RequestOptions.DEFAULT);
            if (!existsResponse) {
                // 创建索引
                CreateIndexRequest createRequest = new CreateIndexRequest("jd_commodity");
                restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
            }
            // 批量请求
            BulkRequest bulkRequest = new BulkRequest();
            for (Content content: contents) {
                bulkRequest.add(new IndexRequest("jd_commodity").source(JSON.toJSONString(content), XContentType.JSON));
            }
            // 发送请求
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                return "failed";
            }
        } catch (IOException e) {
            return "failed";
        }
        return "successful";
    }

    @Override
    public List<Map<String, Object>> searchPage(String keyword, int from, int size) {
        // 创建搜索请求
        SearchRequest searchRequest = new SearchRequest("jd_commodity");
        // 构建搜索条件
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", keyword);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchQueryBuilder);
        // 分页
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        try {
            // 发送请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            List<Map<String, Object>> mapList = new ArrayList<>();
            for (SearchHit hit: searchResponse.getHits()) {
                // 高亮替换
                HighlightField name = hit.getHighlightFields().get("name");
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                if (name != null) {
                    Text[] fragments = name.fragments();
                    String temp = "";
                    for (Text text: fragments) {
                        temp += text;
                    }
                    sourceAsMap.put("name", temp);
                }
                mapList.add(sourceAsMap);
            }
            return mapList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
