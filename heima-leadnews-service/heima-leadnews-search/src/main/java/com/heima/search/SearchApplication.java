package com.heima.search;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableAsync
public class SearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class,args);
    }

    //ES search client接口
    @Bean
    public RestHighLevelClient setClient(){
        return new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.200.128:9200")));
    }
}
