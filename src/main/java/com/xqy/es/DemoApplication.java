package com.xqy.es;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DeDemoApplicationmo
 *
 * @author xqy
 * @date 2020/1/1
 */

@Slf4j
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        //解决netty冲突
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(DemoApplication.class, args);
    }

}
