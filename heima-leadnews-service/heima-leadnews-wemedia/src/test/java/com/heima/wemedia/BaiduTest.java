package com.heima.wemedia;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.audit.baidu.BaiduImageScan;
import com.heima.audit.baidu.BaiduTextScan;
import com.heima.file.service.FileStorageService;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.wemedia.mapper.WmMaterialMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

@SpringBootTest
@Slf4j
public class BaiduTest {

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    @Autowired
    private BaiduImageScan baiduImageScan;

    @Autowired
    private BaiduTextScan baiduTextScan;

    @Autowired
    private FileStorageService fileStorageService;

    private RestHighLevelClient client;

    @BeforeEach
    public void setClient(){
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.200.128:9200")));
    }
    @AfterEach
    public void cleanClient() throws IOException {
        client.close();
    }

    //文本审核
    @Test
    public void textScan(){
        Integer integer = baiduTextScan.textScan("针孔摄像,私人侦探");
        if(integer==1){//合规
            log.info("内容合规");
        }
        if(integer==2){//内容不合规
            log.info("内容不合规");
        }
        if(integer==3){//内容疑似违规
            log.info("内容疑似违规，需要人工审核");
        }
        if(integer==4){//内容审核失败
            log.info("内容审核失败");
        }


    }


    @Test
    public void imageScan(){
        //下载图片
        byte[] bytes = fileStorageService.downLoadFile("http://192.168.200.128:9000/leadnews/2023/06/17/01c991a1126543e6a358a89bfdcac3ee.jpg");
        //审核图片
        Integer integer = baiduImageScan.imageScan(bytes);
        if(integer==1){//合规
            log.info("内容图片合规");
        }
        if(integer==2){//内容不合规
            log.info("内容图片不合规");
        }
        if(integer==3){//内容疑似违规
            log.info("内容图片疑似违规，需要人工审核");
        }
        if(integer==4){//内容审核失败
            log.info("内容图片审核失败");
        }
    }

    @Test
    public void JsonArrayGuess(){
        String string = "[{\"type\":\"image\",\"value\":\"http://192.168.200.196:9000/leadnews/2022/11/02/f27ab4c0-b6b8-4f15-953b-6a0c45e54ac4.jpg\"},{\"type\":\"text\",\"value\":\"我不是一个标题党，aaa我不是一个标题党，aaa我不是一个标题党，aaa我不是一个标题党，aaa我不是一个标题党，aaa我不是一个标题党，aaa\"}]";
        // Map<String,Object> map = new HashMap<>();
        List<Map> maps = JSONArray.parseArray(string, Map.class);
        System.out.println(maps);
        // int length = Array.getLength(parse);
        // System.out.println(length);
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        // List<Array> parse1 = (List)JSONArray.parse(string);
        // System.out.println(parse1);
    }


    @Test
    public void kk() {

        String string = "[{\"type\":\"text\",\"value\":\"今天是一个美好的日子，太阳高高照今天是一个美好的日子，太阳高高照今天是一个美好的日子，太阳高高照今天是一个美好的日子，太阳高高照今天是一个美好的日子，太阳高高照\"},{\"type\":\"image\",\"value\":\"http://192.168.200.128:9000/leadnews/2022/11/02/f27ab4c0-b6b8-4f15-953b-6a0c45e54ac4.jpg\"},{\"type\":\"image\",\"value\":\"http://192.168.200.196:9000/leadnews/2022/11/02/b78fa4cf-2834-4451-874a-4cd5a5750f13.jpg\"}]";
        //文章内容和素材库关联
        StringBuilder sb = new StringBuilder();
        List<String> imgList = new ArrayList<>();
        //解析JSONArray
        List<Map> maps = JSONArray.parseArray(string, Map.class);
        //遍历, 分别将图片和文字提取
        maps.forEach((mapItem)->{
            String type = (String) mapItem.get("type");
            if ("text".equals(type)){
                sb.append(mapItem.get("value"));
            }
            if("image".equals(type)){
                imgList.add((String) mapItem.get("value"));
            }
        });

        LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmMaterial::getId,73);
        WmMaterial wmMaterial = wmMaterialMapper.selectOne(wrapper);
        System.out.println(imgList);

        String s = imgList.get(0);
        System.out.println(s);
        String url = wmMaterial.getUrl();
        System.out.println(url);

        if(s.equals(url)){
            System.out.println("!!!");
        }

    }
    @Test
    public void bulkElastic(){

    }

}
