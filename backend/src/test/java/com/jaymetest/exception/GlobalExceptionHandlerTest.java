package com.jaymetest.exception;

import com.jaymetest.model.dto.R;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    @Test
    void returnsBadRequestForUnknownEnumOrMalformedJson() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        R<Void> result = handler.handleUnreadableMessage(exception);

        assertEquals(400, result.getCode());
        assertEquals("请求参数格式错误", result.getMsg());
    }
}
