package com.heima.article;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ArticleMapper;
import com.heima.model.article.pojos.ApArticle;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @Description ElasticSearch
 * @Author Zhilin
 * @Date 2023-11-07
 */
@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class ElasticSearch {

    @Resource
    private ArticleMapper articleMapper;

    private RestHighLevelClient client;

    @BeforeEach
    public void setClient(){
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.200.128:9200")));
    }
    @AfterEach
    public void cleanClient() throws IOException {
        client.close();
    }

    // 批量从mySQL读取到Elasticsearch
    @Test
    public void bulkElastic() throws IOException {

        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
        List<ApArticle> apArticles =  articleMapper.listAll();
        // List<ApArticle> apArticles = articleMapper.selectList(wrapper);

        BulkRequest request = new BulkRequest("app_info_article");
        apArticles.forEach((article)->{
            String string = JSON.toJSONString(article);
            IndexRequest indexRequest = new IndexRequest("app_info_article").id(article.getId().toString());
            indexRequest.source(string, XContentType.JSON);
            request.add(indexRequest);
        });
        client.bulk(request, RequestOptions.DEFAULT);

    }

}
