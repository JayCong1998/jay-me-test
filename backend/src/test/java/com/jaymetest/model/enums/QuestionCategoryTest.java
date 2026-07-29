package com.jaymetest.model.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionCategoryTest {

    @Test
    void exposesTheFourSupportedQuestionCategories() {
        Set<String> categories = Arrays.stream(QuestionCategory.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("LYRICS", "WORKS", "SCREEN", "KNOWLEDGE"), categories);
    }
}
