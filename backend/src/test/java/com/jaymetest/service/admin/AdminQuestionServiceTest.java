package com.jaymetest.service.admin;

import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.entity.Question;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminQuestionServiceTest {

    @Mock
    private QuestionMapper questionMapper;

    @Test
    void rebalanceMovesOverrepresentedAnswersAndPreservesCorrectOptionText() {
        Question first = question(1L, "A", "correct-1");
        Question second = question(2L, "A", "correct-2");
        Question third = question(3L, "A", "correct-3");
        Question fourth = question(4L, "D", "correct-4");
        when(questionMapper.selectList(any())).thenReturn(List.of(first, second, third, fourth));
        AdminQuestionService service = new AdminQuestionService(questionMapper);

        var result = service.rebalanceOptions();

        assertEquals(2, result.adjustedCount());
        assertEquals(1L, result.answerDistribution().get("A"));
        assertEquals(1L, result.answerDistribution().get("B"));
        assertEquals(1L, result.answerDistribution().get("C"));
        assertEquals(1L, result.answerDistribution().get("D"));
        assertTrue(correctTextAt(first).startsWith("correct-"));
        assertTrue(correctTextAt(second).startsWith("correct-"));
        assertTrue(correctTextAt(third).startsWith("correct-"));
        assertEquals("correct-4", correctTextAt(fourth));

        ArgumentCaptor<Question> updatedQuestion = ArgumentCaptor.forClass(Question.class);
        verify(questionMapper, atLeastOnce()).updateById(updatedQuestion.capture());
        assertTrue(updatedQuestion.getAllValues().stream()
                .allMatch(question -> correctTextAt(question).startsWith("correct-")));
    }

    private Question question(long id, String correctOption, String correctText) {
        Question question = new Question();
        question.setId(id);
        question.setOptionA("A".equals(correctOption) ? correctText : "wrong-a-" + id);
        question.setOptionB("B".equals(correctOption) ? correctText : "wrong-b-" + id);
        question.setOptionC("C".equals(correctOption) ? correctText : "wrong-c-" + id);
        question.setOptionD("D".equals(correctOption) ? correctText : "wrong-d-" + id);
        question.setCorrectOption(correctOption);
        return question;
    }

    private String correctTextAt(Question question) {
        return switch (question.getCorrectOption()) {
            case "A" -> question.getOptionA();
            case "B" -> question.getOptionB();
            case "C" -> question.getOptionC();
            case "D" -> question.getOptionD();
            default -> throw new IllegalArgumentException("Unexpected option: " + question.getCorrectOption());
        };
    }
}
