package com.jaymetest.service.admin;

import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.mapper.UserMapper;
import com.jaymetest.model.admin.AdminDashboardOverview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;
    private final GameRecordMapper gameRecordMapper;

    public AdminDashboardOverview getOverview() {
        Map<String, Long> modeDistribution = new LinkedHashMap<>();
        gameRecordMapper.selectModeDistribution().forEach(row -> {
            Object mode = row.get("mode");
            Object count = row.get("cnt");
            if (mode != null && count instanceof Number number) {
                modeDistribution.put(mode.toString(), number.longValue());
            }
        });

        return AdminDashboardOverview.builder()
                .totalQuestions(questionMapper.countTotal())
                .totalUsers(userMapper.selectCount(null))
                .totalRecords(gameRecordMapper.countTotal())
                .todayRecords(gameRecordMapper.countToday())
                .averageCorrectCount(gameRecordMapper.selectAverageCorrectCount())
                .modeDistribution(modeDistribution)
                .build();
    }
}
