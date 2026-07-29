package com.jaymetest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 新开局使用的服务端玩法规则。客户端不能指定题量、难度或通关门槛。
 */
@Data
@ConfigurationProperties(prefix = "game")
public class GameRuleProperties {

    private Classic classic = new Classic();
    private Album album = new Album();
    private Abyss abyss = new Abyss();

    @Data
    public static class Classic {
        private int questionCount = 10;
        private int revivalCount = 1;
        private double easyWeight = 0.6;
    }

    @Data
    public static class Album {
        private int questionCount = 10;
        private int passThreshold = 8;
        private int revivalCount = 0;
    }

    @Data
    public static class Abyss {
        private int batchSize = 5;
    }
}
