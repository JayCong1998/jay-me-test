package com.jaymetest.service.admin;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminQuestionDetailServiceTest {

    @Mock
    private QuestionMapper questionMapper;

    @Test
    void getThrowsNotFoundWhenQuestionDoesNotExist() {
        when(questionMapper.selectById(99L)).thenReturn(null);
        AdminQuestionService service = new AdminQuestionService(questionMapper);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.get(99L));

        assertEquals(404, exception.getCode());
        assertEquals("题目不存在", exception.getMessage());
    }
}
