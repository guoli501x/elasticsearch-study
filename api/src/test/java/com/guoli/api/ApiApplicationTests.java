package com.guoli.api;

import com.alibaba.fastjson.JSON;
import com.guoli.api.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ApiApplicationTests {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    void contextLoads() {
    }

    @Test
    void createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("springboot");
        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response.index());
        restHighLevelClient.close();
    }

    @Test
    void existIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("springboot");
        boolean response = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(response);
        restHighLevelClient.close();
    }

    @Test
    void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("springboot");
        AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
        restHighLevelClient.close();
    }

    @Test
    void createId() throws IOException {
        GetIndexRequest existsRequest = new GetIndexRequest("springboot");
        boolean existsResponse = restHighLevelClient.indices().exists(existsRequest, RequestOptions.DEFAULT);
        if (!existsResponse) {
            CreateIndexRequest createRequest = new CreateIndexRequest("springboot");
            restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
        }
        User user = new User("张三", 3);
        user.setAge("aaa".equals(user.getName()) ? 0 : 1);
        IndexRequest indexRequest = new IndexRequest("springboot");
        indexRequest.id("1");
        indexRequest.timeout(TimeValue.timeValueSeconds(1));
        indexRequest.source(JSON.toJSONString(user), XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse.status());
        restHighLevelClient.close();
    }

    @Test
    void existsId() throws IOException {
        GetRequest getRequest = new GetRequest("springboot", "1");
        // 不获取返回的_source的上下文，效率更快
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
        restHighLevelClient.close();
    }

    @Test
    void getId() throws IOException {
        GetRequest getRequest = new GetRequest("springboot", "1");
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse.toString());
        System.out.println(getResponse.getIndex());
        System.out.println(getResponse.getId());
        System.out.println(getResponse.getFields());
        System.out.println(getResponse.getSeqNo());
        System.out.println(getResponse.getPrimaryTerm());
        System.out.println(getResponse.getSourceAsString());
        restHighLevelClient.close();
    }

    @Test
    void updateId() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("springboot", "1");
        User user = new User("李四", 4);
        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
        updateRequest.timeout("1s");
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse.toString());
        System.out.println(updateResponse.status());
        restHighLevelClient.close();
    }

    @Test
    void deleteId() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("springboot", "1");
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
        System.out.println(deleteResponse.toString());
        restHighLevelClient.close();
    }

    /**
     * 批处理
     *
     * @throws IOException io异常
     */
    @Test
    void bulkCreate() throws IOException {
        GetIndexRequest existsRequest = new GetIndexRequest("springboot");
        boolean existsResponse = restHighLevelClient.indices().exists(existsRequest, RequestOptions.DEFAULT);
        if (!existsResponse) {
            CreateIndexRequest createRequest = new CreateIndexRequest("springboot");
            restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
        }
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        for (int i = 0; i < 10; i++) {
            User user = new User(String.valueOf(i), i);
            bulkRequest.add(new IndexRequest("springboot").id(String.valueOf(i)).source(JSON.toJSONString(user), XContentType.JSON));
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.status());
        System.out.println(bulkResponse.hasFailures()); // 是否失败
        restHighLevelClient.close();
    }

    @Test
    void search() throws IOException {
        SearchRequest searchRequest = new SearchRequest("springboot");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        MatchQueryBuilder query = QueryBuilders.matchQuery("name", "1");
        sourceBuilder.query(query);
        sourceBuilder.timeout(TimeValue.timeValueSeconds(10));
        // 分页
        sourceBuilder.from(0);
        sourceBuilder.size(2);
        // 高亮必须要有sourceBuilder.query(query);查询条件，且高亮字段必须与查询字段相同
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.preTags();
        highlightBuilder.postTags();
        sourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse.status());
        System.out.println(JSON.toJSONString(searchResponse));
        for (SearchHit searchHit: searchResponse.getHits().getHits()) {
            System.out.println(searchHit.toString());
        }
        restHighLevelClient.close();
    }
}
