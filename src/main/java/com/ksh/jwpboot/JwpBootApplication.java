package com.ksh.jwpboot;

import com.ksh.jwpboot.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(AppProperties.class)
public class JwpBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwpBootApplication.class, args);
    }

}
