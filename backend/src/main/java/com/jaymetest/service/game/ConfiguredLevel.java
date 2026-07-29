package com.jaymetest.service.game;

public record ConfiguredLevel(String key, String title, String description) implements LevelInfo {
    @Override public String name() { return key; }
    @Override public String getTitle() { return title; }
    @Override public String getDescription() { return description; }
}
