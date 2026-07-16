package com.jaymetest.service;

import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.model.dto.GameSubmitRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private GameRecordMapper gameRecordMapper;

    @InjectMocks
    private StatsService statsService;

    @Test
    void testSubmitResult() {
        when(gameRecordMapper.selectOne(any())).thenReturn(null);
        when(gameRecordMapper.insert(any(com.jaymetest.model.entity.GameRecord.class))).thenReturn(1);
        when(gameRecordMapper.countTotal()).thenReturn(100L);
        when(gameRecordMapper.countByCorrectCountLessThan(eq(7))).thenReturn(65L);

        GameSubmitRequest request = new GameSubmitRequest();
        request.setRoundId("test-uuid");
        request.setCorrectCount(7);
        request.setTimeSpentSecs(120);
        request.setUsedRevival(0);

        GameResultDTO result = statsService.submitResult(request);

        assertNotNull(result);
        assertEquals(70, result.getScore());
        assertEquals(7, result.getCorrectCount());
        assertEquals(10, result.getTotalQuestions());
    }
}
