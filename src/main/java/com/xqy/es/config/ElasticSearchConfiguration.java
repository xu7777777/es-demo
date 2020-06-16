package com.xqy.es.config;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchConfiguration implements InitializingBean {
    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(System.getProperty("es.set.netty.runtime.available.processors"));
    }
}
