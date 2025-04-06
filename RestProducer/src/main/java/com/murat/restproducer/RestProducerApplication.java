package com.murat.restproducer;

import com.murat.restproducer.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RsaKeyProperties.class)
@SpringBootApplication
public class RestProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestProducerApplication.class, args);
    }

}
