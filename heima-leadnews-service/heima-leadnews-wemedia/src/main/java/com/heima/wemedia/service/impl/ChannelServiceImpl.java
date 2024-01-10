package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.ChannelService;
import org.springframework.stereotype.Service;

@Service
public class ChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements ChannelService {
}
