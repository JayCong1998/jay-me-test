package com.jaymetest.service.admin;

import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.mapper.UserMapper;
import com.jaymetest.model.admin.AdminDashboardOverview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private GameRecordMapper gameRecordMapper;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void overviewIncludesCoreCountsAndModeDistribution() {
        when(questionMapper.countTotal()).thenReturn(70L);
        when(userMapper.selectCount(null)).thenReturn(12L);
        when(gameRecordMapper.countTotal()).thenReturn(100L);
        when(gameRecordMapper.countToday()).thenReturn(9L);
        when(gameRecordMapper.selectAverageCorrectCount()).thenReturn(7.5);
        when(gameRecordMapper.selectModeDistribution()).thenReturn(List.of(
                Map.of("mode", "CLASSIC", "cnt", 80L),
                Map.of("mode", "ALBUM", "cnt", 20L)
        ));

        AdminDashboardOverview overview = adminDashboardService.getOverview();

        assertEquals(70L, overview.getTotalQuestions());
        assertEquals(12L, overview.getTotalUsers());
        assertEquals(100L, overview.getTotalRecords());
        assertEquals(9L, overview.getTodayRecords());
        assertEquals(7.5, overview.getAverageCorrectCount());
        assertEquals(80L, overview.getModeDistribution().get("CLASSIC"));
        assertEquals(20L, overview.getModeDistribution().get("ALBUM"));
    }
}
