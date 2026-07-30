package com.jaymetest.model.admin;

import java.util.Map;

public record QuestionOptionRebalanceResponse(int adjustedCount, Map<String, Long> answerDistribution) {
}
