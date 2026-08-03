package com.jaymetest.service.game.support;

import com.jaymetest.model.dto.QuestionDTO;
import com.jaymetest.model.entity.Question;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目 Entity → DTO 转换工具 — 消除 QuestionService 中的重复映射代码
 */
public final class QuestionAssembler {

    private QuestionAssembler() {
        // 工具类禁止实例化
    }

    public static QuestionDTO toDTO(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setCategory(q.getCategory());
        dto.setDifficulty(q.getDifficulty());
        dto.setQuestionText(q.getQuestionText());
        dto.setOptions(q.getOptionsAsList());
        return dto;
    }

    public static List<QuestionDTO> toDTOList(List<Question> questions) {
        return questions.stream()
                .map(QuestionAssembler::toDTO)
                .collect(Collectors.toList());
    }
}
