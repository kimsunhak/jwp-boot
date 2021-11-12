package com.ksh.jwpboot.controller;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class TestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void test() throws Exception {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replaceAll("-", "");
        logger.info(uuid);
    }
}
