package com.heima.wemedia.service.impl;

import com.heima.audit.baidu.BaiduTextScan;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.mapper.WmNewsMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @Description ExamCallable
 * @Author Zhilin
 * @Date 2023-11-07
 */
@Slf4j
@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamCallable implements Callable {

    private Map<String, Object> txtImgMap;

    //传参数
    private WmNews wmNews;

    private WmNewsMapper wmNewsMapper ;

    @Override
    public Object call()  {

        // 审核
        // 文字
        String text = (String) txtImgMap.get("text");
        BaiduTextScan baiduTextScan = new BaiduTextScan();
        baiduTextScan.setApiKey("XqWNiyhdgc2vrE7BsIWNo7XV");
        baiduTextScan.setSecretKey("uwsFH97SNxbxMavvTvUa9UjyQQmojYik");
        Integer filterResult = baiduTextScan.textScan(text);
        log.info("百度云检测结果为: " + filterResult);
        if (filterResult == 2) {
            return ResponseResult.errorResult(404, "上传失败,色情信息!");
        }
        if (filterResult == 3 || filterResult == 4) {
            return ResponseResult.errorResult(405, "转人工审核");
        }
        if(filterResult ==1 ){

            wmNews.setStatus((short) 9);
            wmNewsMapper.updateById(wmNews);

        }
        return null;
    }

}
