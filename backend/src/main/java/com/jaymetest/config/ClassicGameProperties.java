package com.jaymetest.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "game.classic")
public class ClassicGameProperties {
    private int questionCount = 10;
    private double easyWeight = 0.6;
    private List<LevelRangeProperties> levels;
    @PostConstruct public void validate() {
        if (questionCount < 1 || questionCount > 100 || !Double.isFinite(easyWeight) || easyWeight < 0 || easyWeight > 1) throw new IllegalStateException("经典模式基础配置非法");
        LevelRangeValidator.validate(levels, 100, false, "经典模式");
    }
}
