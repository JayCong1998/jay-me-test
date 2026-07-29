package com.jaymetest.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "game.abyss")
public class AbyssGameProperties {
    private int batchSize = 5;
    private List<LevelRangeProperties> levels;
    @PostConstruct public void validate() {
        if (batchSize < 1 || batchSize > 50) throw new IllegalStateException("深渊模式 batchSize 必须在 1-50 之间");
        LevelRangeValidator.validate(levels, 0, true, "深渊模式");
    }
}
