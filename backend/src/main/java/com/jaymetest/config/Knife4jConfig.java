package com.jaymetest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j OpenAPI 文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("杰迷结业考试 API")
                        .version("1.0.0")
                        .description("面向周杰伦粉丝的 H5 答题应用后端接口")
                        .contact(new Contact()
                                .name("jay-me-test")
                                .url("https://github.com/jay-me-test")));
    }
}
