package com.jaymetest.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "game.abyss")
public class AbyssGameProperties {
    private int batchSize = 5;
    private int revivalCount = 1;
    private List<LevelRangeProperties> levels;
    @PostConstruct public void validate() {
        if (batchSize < 1 || batchSize > 50 || revivalCount < 0 || revivalCount > 3) throw new IllegalStateException("深渊模式基础配置非法");
        LevelRangeValidator.validate(levels, 0, true, "深渊模式");
    }
}
