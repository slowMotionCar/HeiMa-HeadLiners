package com.heima.wemedia.service;

public interface WmAutoScanService {
    /**
     * 审核自媒体文章
     * @param newsId 自媒体文章id
     */
    public void autoScanWmNews(Integer newsId);
}
