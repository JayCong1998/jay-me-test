package com.jaymetest.config;

import lombok.Data;

@Data
public class LevelRangeProperties {
    private String key;
    private String title;
    private String description;
    private Integer min;
    private Integer max;
}
