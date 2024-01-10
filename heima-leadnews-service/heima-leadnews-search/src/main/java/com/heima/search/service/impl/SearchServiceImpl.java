package com.heima.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.SearchService;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 基本搜索业务
     *
     * @return
     */
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
            // 解析
            SearchHits hits = response.getHits();
            TotalHits totalHits = hits.getTotalHits();
            List list = new ArrayList<>();
            SearchHit[] hitsArray = hits.getHits();
            for (SearchHit item : hitsArray) {
                // System.out.println(item);
                String sourceAsString = item.getSourceAsString();
                ApArticle apArticle = JSON.parseObject(sourceAsString, ApArticle.class);
                list.add(apArticle);
            }

            PageResponseResult pageResponseResult = new PageResponseResult();
            pageResponseResult.setTotal((int) totalHits.value);
            pageResponseResult.setData(list);
            pageResponseResult.setCurrentPage(userSearchDto.getPageNum());
            pageResponseResult.setSize(userSearchDto.getPageSize());

            return pageResponseResult;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("ES分页异常");
        }

    }


    /**
     * 自动补全功能
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult load(UserSearchDto dto) {
        // TODO
        return null;
    }
}
