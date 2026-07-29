package com.jaymetest.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "game.album")
public class AlbumGameProperties {
    private int questionCount = 10;
    private int passAccuracy = 80;
    private List<LevelRangeProperties> levels;
    @PostConstruct public void validate() {
        if (questionCount < 1 || questionCount > 100 || passAccuracy < 0 || passAccuracy > 100) throw new IllegalStateException("专辑模式基础配置非法");
        LevelRangeValidator.validate(levels, 100, false, "专辑模式");
    }
}
