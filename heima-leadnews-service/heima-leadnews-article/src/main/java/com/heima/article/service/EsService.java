package com.heima.article.service;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

import java.io.IOException;

/**
 * @Description EsService
 * @Author Zhilin
 * @Date 2023-11-07
 */
public interface EsService {
    void setAll() throws IOException;

    PageResponseResult search(UserSearchDto userSearchDto);
}
