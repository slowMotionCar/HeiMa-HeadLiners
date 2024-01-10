package com.heima.wemedia.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WmAutoScanServiceTest {

    @Autowired
    private WmAutoScanService wmAutoScanService;

    @Test
    void autoScanWmNews() {
        wmAutoScanService.autoScanWmNews(6235);
    }
}