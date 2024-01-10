package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ArticleMapper;
import com.heima.article.service.EsService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description EsServiceImpl
 * @Author Zhilin
 * @Date 2023-11-07
 */
@Service
public class EsServiceImpl implements EsService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RestHighLevelClient client;


    @Override
    public void setAll() throws IOException {

        // LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
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

    @Override
    public PageResponseResult search(UserSearchDto userSearchDto) {

        try {
            String searchWords = userSearchDto.getSearchWords();
            int pageNum = userSearchDto.getPageNum();
            int pageSize = userSearchDto.getPageSize();

            SearchRequest request = new SearchRequest("app_info_article");
            request.source().query(QueryBuilders.matchQuery("title", searchWords));
            request.source().from(pageNum).size(pageSize);

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //解析
            SearchHits hits = response.getHits();
            TotalHits totalHits = hits.getTotalHits();
            List list = new ArrayList<>();
            SearchHit[] hitsArray = hits.getHits();
            for (SearchHit item : hitsArray) {
                String sourceAsString = item.getSourceAsString();
                list.add(sourceAsString);
            }

            PageResponseResult pageResponseResult = new PageResponseResult();
            pageResponseResult.setTotal((int) totalHits.value);
            pageResponseResult.setData(list);

            return pageResponseResult;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("ES分页异常");
        }
    }
}
