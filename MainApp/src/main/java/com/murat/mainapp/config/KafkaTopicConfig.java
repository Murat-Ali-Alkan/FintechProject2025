//package com.murat.mainapp.config;
//
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class KafkaTopicConfig {
//
//    @Bean
//    public NewTopic newTopic() {
//        return new NewTopic("rate-values", 3, (short) 1);
//    }
//}