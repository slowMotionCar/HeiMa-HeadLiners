package com.heima.search.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.constants.ArticleConstants;
import com.heima.model.common.constants.WmNewsMessageConstants;
import com.heima.model.search.dtos.SearchArticleVo;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SyncArticleListener {

    @Autowired
    private RestHighLevelClient client;



    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = ArticleConstants.ARTICLE_ES_SYNC_QUEUE,durable = "true"),
                    exchange = @Exchange(name = ArticleConstants.ARTICLE_ES_SYNC_TOPIC,type = ExchangeTypes.TOPIC),
                    key = ArticleConstants.ARTICLE_ES_SYNC_ROUTINGKEY
            )
    )
    public void getMessage(ApArticle apArticle) throws IOException {

        String string = JSON.toJSONString(apArticle);
        IndexRequest indexRequest = new IndexRequest("app_info_article").id(String.valueOf(apArticle.getId()));
        indexRequest.source(string,XContentType.JSON);

        client.index(indexRequest,RequestOptions.DEFAULT);




    }
}
