package com.heima.search.service;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

public interface SearchService {

    /**
     * 基本搜索业务
     * @param dto
     * @return
     */
    public PageResponseResult search(UserSearchDto dto);

    /**
     * 自动补全功能
     * @return
     */
    ResponseResult load(UserSearchDto dto);
}
