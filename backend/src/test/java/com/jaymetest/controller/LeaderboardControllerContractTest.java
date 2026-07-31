package com.jaymetest.controller;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderboardControllerContractTest {

    private final String controllerSource = readControllerSource();

    @Test
    void leaderboardPaginationUsesPageAndSizeWithoutLegacyLimit() {
        assertTrue(controllerSource.contains("@RequestParam(defaultValue = \"1\") int page"));
        assertTrue(controllerSource.contains("@RequestParam(defaultValue = \"20\") int size"));
        assertFalse(controllerSource.contains("RequestParam(defaultValue = \"50\") int limit"));
        assertFalse(controllerSource.contains("queryLimit"));
    }

    private static String readControllerSource() {
        try {
            return Files.readString(Path.of("src/main/java/com/jaymetest/controller/LeaderboardController.java"));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read LeaderboardController source", e);
        }
    }
}
